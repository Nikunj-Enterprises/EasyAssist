(function(){
  
  var chat = {
    messageToSend: '',
    messageResponses: '',
	stompClient: Stomp.over(new SockJS('/assistance-websocket')),
    init: function() {
      this.cacheDOM();
      this.bindEvents();
      this.render();
      this.connect();
    },
    subscribe: function(topic, callback){
       this.stompClient.subscribe(topic, callback);
    },
	connect: function() {
        //To Revisit		
	    $('#clientId').val('school');
	    var self = this;
        //var socket = new SockJS('/assistance-websocket');
        function callback(message){
            console.log("In call back:: "+message);
            var msgObj = JSON.parse(message.body);
            var repliedBy = msgObj.repliedBy;
            var repliedTo = msgObj.repliedTo;
            var content = msgObj.message;
            if(repliedBy !== null && repliedBy !== ""){
               // To Revisit
               console.log("Received message ::"+content);
               var templateResponse = Handlebars.compile( $("#message-response-template").html());
               var contextResponse = {
                         response: content,
                         time: self.getCurrentTime()
                       };
               self.$chatHistoryList.append(templateResponse(contextResponse));
               self.scrollToBottom();
            }
        };
        //this.stompClient = Stomp.over(socket);
        this.stompClient.connect({}, function (frame) {                  
			var p2pTopic = "/topic/assistance/"+$("#clientId").val();
			console.log("subscribing to :"+p2pTopic);
			this.subscribe(p2pTopic, callback);
        });
	},
    cacheDOM: function() {
      this.$chatHistory = $('.chat-history');
      this.$button = $('button');
      this.$textarea = $('#message-to-send');
      this.$chatHistoryList =  this.$chatHistory.find('ul');
    },
    bindEvents: function() {
      this.$button.on('click', this.addMessage.bind(this));
      this.$textarea.on('keyup', this.addMessageEnter.bind(this));
    },
    render: function() {
      this.scrollToBottom();
      if (this.messageToSend.trim() !== '') {
        var template = Handlebars.compile( $("#message-template").html());
        var context = { 
          messageOutput: this.messageToSend,
          time: this.getCurrentTime()
        };

        this.$chatHistoryList.append(template(context));
        this.scrollToBottom();
        this.$textarea.val('');
        
        // responses
//        var templateResponse = Handlebars.compile( $("#message-response-template").html());
//        var contextResponse = {
//          response: this.messageResponses,
//          time: this.getCurrentTime()
//        };
//
//        setTimeout(function() {
//          this.$chatHistoryList.append(templateResponse(contextResponse));
//          this.scrollToBottom();
//        }.bind(this), 1500);
        
      }
      
    },
    
    addMessage: function() {
      this.messageToSend = this.$textarea.val()
	  //To Revisit
	  this.stompClient.send("/assistance/"+$("#clientId").val()+"/chat/bot4school",{}, this.messageToSend);
      this.render();         
    },
    addMessageEnter: function(event) {
        // enter was pressed
        if (event.keyCode === 13) {
          this.addMessage();
        }
    },
    scrollToBottom: function() {
       this.$chatHistory.scrollTop(this.$chatHistory[0].scrollHeight);
    },
    getCurrentTime: function() {
      return new Date().toLocaleTimeString().
              replace(/([\d]+:[\d]{2})(:[\d]{2})(.*)/, "$1$3");
    },
    getRandomItem: function(arr) {
      return arr[Math.floor(Math.random()*arr.length)];
    }
    
  };
  
  chat.init();
  
  var searchFilter = {
    options: { valueNames: ['name'] },
    init: function() {
      var userList = new List('people-list', this.options);
      var noItems = $('<li id="no-items-found">No items found</li>');
      
      userList.on('updated', function(list) {
        if (list.matchingItems.length === 0) {
          $(list.list).append(noItems);
        } else {
          noItems.detach();
        }
      });
    }
  };
  
  searchFilter.init();
  
})();