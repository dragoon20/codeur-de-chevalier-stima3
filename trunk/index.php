<html>
	<head>
		<title> ChickChat </title>
		<link rel="stylesheet" type="text/css" href="css/style.css">
	</head>
	<body>
		<div style="display:block; margin-left:auto; margin-right:auto; width:80%;">
			<img src="img/title.png" style="margin-left:auto; margin-right:auto; ">
		</div>
		<div id="container">
			<div style="display:block; width:20%px;">
				<img id="chicken" src="img/happy.png" style="float:left;"> 
				<!-- macam2 image :  shock,  mad, happy, cry , laugh -->
			</div>
			<form> 
				<textarea id="chat_log" style="background-color:#fff;width:80%; height:400px; display:block; margin-left:auto; margin-right:auto;" readonly="readonly"></textarea>
				<div style="margin-left:21%; margin-right:auto; display:block; width:80%; margin-top:20px;"> 
					<input type="text" id="chat_input" name="chat_input" style="float:left; width: 70%;" placeholder="Ketik pertanyaanmu disini..." onKeyPress="return submitenter(this,event)"> 
					<input type="button" id="chat_submit" value="Kirim" onclick="chat_append();" style="float:left; margin-left:5px;">
				</div>
			</form>
		</div>
	</body>
	
	<script type="text/javascript" src="js/jquery-1.7.2.js"> </script>
	<script type="text/javascript"> 
		function change_emotion(emot) {
			$('#chicken').attr(src,emot+".png");
		}
		
		function chat_append(flag) {
			var newChat = $('#chat_input').val();
			if (flag == 0) 
				$('#chat_log').append("Chick : " + newChat + "\n");
			else 
			if (flag == 1) 
				$('#chat_log').append("Kamu  : " + newChat + "\n");
			$('#chat_input').val("");
			$('#chat_log').scrollTop(1000000000);
		}
		
		function submitenter(myfield,e) {
			var keycode;
			if (window.event) keycode = window.event.keyCode;
			else if (e) keycode = e.which;
			else return true;
			if (keycode == 13) {
				chat_append(1);
				return false;
			}
			else return true;
		}
	</script>
</html>
