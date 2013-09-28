$(function(){
	'use strict';

    var chatSocket = new WebSocket($('form').attr("action"));

    $("[type='submit']").click(function(e){
    	e.preventDefault();

    	chatSocket.send(
    		JSON.stringify({
    			firstName: $("[name='firstName']").val(),
    			lastName: $("[name='lastName']").val()
    		})
    	);
    	return false;
    });

    chatSocket.onmessage = function(event){
		var data = JSON.parse(event.data)
		$(".result").text(data.response);
    };
});