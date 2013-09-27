$(function(){
	'use strict';

    var chatSocket = new WebSocket($('form').attr("action"));

    $("[type='submit']").click(function(e){
    	e.preventDefault();

    	chatSocket.send(
    		JSON.stringify({
    			op1: parseInt($("[name='op1']").val()),
    			op2: parseInt($("[name='op2']").val())
    		})
    	);
    	return false;
    });

    chatSocket.onmessage = function(e){
		var data = JSON.parse(event.data)
		$(".result").text(data.result || data.error);
    };
});