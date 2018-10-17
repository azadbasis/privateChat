var express = require('express'),
    app = express(),
    server = require('http').createServer(app),
    io = require('socket.io').listen(server),
    mongoose = require('mongoose')
users = {};

//  server.listen(3000);
server.listen(3000, () => {
    console.log(`Server is running on port 3000`);
});

mongoose.connect('mongodb://localhost/chat', function (err) {
    if (err) {
        console.log(err);
    } else {
        console.log('connected to mongodb!!');
    }
})

var chatSchema = mongoose.Schema({
    nick: String,
    msg: String,
    created: { type: Date, default: Date.now }
});
var Chat = mongoose.model('Message', chatSchema);


app.get('/', (req, res) => {
    res.sendFile(__dirname + '/index.html');

});

io.sockets.on('connection', function (socket) {

    var query=Chat.find({});
    query.limit(8).sort('-created').exec(function (err, docs) {
             if(err)throw err
             socket.emit('load old message',docs);
        });
    // Chat.find({}, function (err, docs) {
    //      if(err)throw err
    //      socket.emit('load old message',docs);
    // });

    socket.on('new user', function (data, callback) {
        if (data in users) {
            callback(false);
        } else {
            callback(true);
            socket.nickName = data;
            console.log(data);
            users[socket.nickName] = socket;
            updateNickNames();
        }

    });



    function updateNickNames() {
        io.sockets.emit('usernames', Object.keys(users));
    }


    socket.on('disconnect', function (data) {
        if (!socket.nickName) return
        delete users[socket.nickName];
        updateNickNames();
    });

    socket.on('typing', function (data) {
        socket.broadcast.emit('typing', { nick: socket.nickName });
    });

    socket.on('send message', function (data, callback) {
        var msg = data.trim();
        if (msg.substr(0, 3) === '/w ') {
            msg = msg.substr(3);
            var ind = msg.indexOf(' ');
            if (ind != -1) {
                var name = msg.substr(0, ind);
                var msg = msg.substr(ind + 1);
                if (name in users) {
                    //   if(name === 'salam'){
                    //     io.to(`${socket.id}`).emit('hey', 'I just met you');
                    //   }
                    users[name].emit('whisper', { msg: msg, nick: socket.nickName });
                    users[socket.nickName].emit('me', { msg: msg, nick: socket.nickName });
                    console.log('Whisper!');
                } else {
                    callback('Error! Enter a valid user.')
                }

            } else {
                callback('Error! Please, enter a message for your whisper.');
            }

        } else {
            console.log(data);
            var newMessage = new Chat({ msg: msg, nick: socket.nickName });
            newMessage.save(function (err) {
                if (err) throw err;
                io.sockets.emit('new message', { msg: msg, nick: socket.nickName });
            })
        }

    });

});