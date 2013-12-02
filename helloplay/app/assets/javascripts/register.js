$(function() {
	
	$('#submit').click(addUser)
	
	fetchAllusers();
})
	
function errorAjax(err){
	alert(err.responseText);
//	console.log(err.responseText);
}

function refreshUsers(users){
	var userList = $('#user-list');
	userList.empty();
	for(i=0;i<users.length;i++){
		var user = users[i];
		userList.append("<li><span id=\"name"+user.id+"\" onclick=\"exchangeEditable('"+user.id+"',this,'name')\">"+user.name+"</span>" +
				"\\<span id=\"email"+user.id+"\" onclick=\"exchangeEditable('"+user.id+"',this,'email')\">"+user.email+"</span>" +
				"(<span id=\"password"+user.id+"\" onclick=\"exchangeEditable('"+user.id+"',this,'password')\">"+user.password+"</span>)" +
				"&nbsp;&nbsp;<a href=\"javascript:deleteUser('"+user.id+"')\" style=\"color:red;\">X</a>" +
				"&nbsp;<button id=\"button"+user.id+"\">update</button></li>");
	}
}

var ajaxResponse = {
		success : refreshUsers,
		error : errorAjax
	};

function deleteUser(id){
	jsRoutes.controllers.UserController.deleteUser(id).ajax(ajaxResponse)
}

function fetchAllusers(){
	jsRoutes.controllers.UserController.allUsers().ajax(ajaxResponse)	
}

function addUser(){
	jsRoutes.controllers.UserController.addUser().ajax({
		success : refreshUsers,
		error : errorAjax,
		data : $("#register-form").serialize()
	})
}

function updateUser(id,key,value){
	jsRoutes.controllers.UserController.updateUser(id,key,value).ajax(ajaxResponse)
}

function exchangeEditable(id,el,token){
	var nodeI=el.parentNode, inputC=document.createElement('input'), text=el.innerHTML;
	el.style.font='.9em "ms sans serif", "sans"'
	el.innerHTML+='\x20'
	with (inputC){
		setAttribute('value', text, 0)
		setAttribute('size', text.length-1, 0)
		style.width=document.all&&!window.opera? el.offsetWidth-2+'px' : el.offsetWidth+2+'px'
		setAttribute('type', 'text', 0)
		setAttribute('id', el.id, 0)
	}
	nodeI.replaceChild(inputC, el);
	var b = document.getElementById('button'+id);
	b.onclick = function(){
		updateUser(id,token,inputC.value);	
	} 	
}
