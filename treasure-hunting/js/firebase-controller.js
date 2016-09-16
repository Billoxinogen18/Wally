FirebaseController = function () {
    var dbref;

    // Initialize Firebase
    var config = {
        apiKey: "AIzaSyDb4WMoWMAFBZodIdDj0ueWOaD2pImRwT4",
        authDomain: "wally-virtual-notes-d73d7.firebaseapp.com",
        databaseURL: "https://wally-virtual-notes-d73d7.firebaseio.com",
        storageBucket: "wally-virtual-notes-d73d7.appspot.com"
    };

    firebase.initializeApp(config);

    dbref = firebase.database().ref().child('Develop/');

    this.getNotes = function (onLoadEach) {
        var colRef = dbref.child("Contents/Puzzles");
        colRef.once("value", function (snapshot) {
            var puzzles = Object.keys(snapshot.val());
            $.each(puzzles, function(i, value){
                var contents = dbref.child("Contents/" + value.replace(":", "/"));
                contents.once("value", function (snapshot) {
                    var note = snapshot.val();
                    note.id = value;
                    onLoadEach(value, note);
                })
            });
        });
    };

    this.getGames = function (onLoad) {

    };

    this.saveGame = function (game) {
        var colRef = dbref.child("Games");

        var usedPuzzles = game.getNotes();
        var connections = game.getConnections();
        var startNotes = game.getStartNotes();

        this.removePuzzles(usedPuzzles);
        this.updateConnections(connections);

        for(var i=0; i<startNotes.length; i++){
            startNotes[i]["PuzzleData"]['Connections'] = connections[startNotes[i].id];
        }

        var newStartNotes = this.markAsPublic(startNotes);

        colRef.push({"name": game.getName(), "startNotes": newStartNotes});
    };


    this.updateConnections = function(connections){
        var notes = Object.keys(connections);
        for(var i=0; i<notes.length; i++){
            connections[notes[i]] = this.updateConnection(notes[i], connections[notes[i]]);
        }
    };

    this.updateConnection = function(note_id, connections){
        for(var i=0; i<connections.length; i++){
            connections[i] = connections[i].id;
        }
        var content = dbref.child("Contents/" + note_id.replace(":", "/") + "/PuzzleData/Connections");
        content.set(connections);
        return connections;
    };

    this.removePuzzles = function(puzzles){
        for(var i=0; i<puzzles.length; i++){
            dbref.child("Contents/Puzzles/" + puzzles[i].id).remove();
        }
    };

    this.markAsPublic = function(notes){
        var res = [];
        for(var i=0; i<notes.length; i++){
            var note = notes[i];
            var id = note.id;
            var small_id = id.split(":");
            small_id = small_id[small_id.length-1];
            delete note.id;

            note.publicity = 0;

            res.push(small_id);

            dbref.child("Contents/" + id.replace(":", "/")).remove();
            dbref.child("Contents/Public/" + small_id).set(note);
            dbref.child("Rooms/" + note['roomId'] + "/Contents/" + id).remove();
            dbref.child("Rooms/" + note['roomId'] + "/Contents/Public:" + small_id).set(true);
        }

        return res;
    }
};