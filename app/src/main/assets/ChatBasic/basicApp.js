var express = require('express'),
   app = express(),
   server = require('http').createServer(app),
   io = require('socket.io').listen(server)
   nickNames=[];
    
 //  server.listen(3000);
   server.listen(3000, () => {
    console.log(`Server is running on port 3000`);
});
app.get('/', (req, res) => {
res.sendFile(__dirname+'/index.html');

});

io.sockets.on('connection',function(socket){

     socket.on('new user',function(data,callback){

        if(nickNames.indexOf(data)!=-1){
            callback(false);
        }else{
            callback(true);
            socket.nickName=data;
            nickNames.push(socket.nickName)
           updateNickNames();
        }

     })

     function updateNickNames(){
       io.sockets.emit('usernames',nickNames);
        }

     socket.on('disconnect',function(data){

        if(!socket.nickName)return
        nickNames.splice(nickNames.indexOf(socket.nickName),1);

        updateNickNames();

     });


    socket.on('send message',function(data){
         console.log(data);
        io.sockets.emit('new message',{msg:data,nick:socket.nickName});

    });

});