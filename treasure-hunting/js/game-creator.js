if (!String.prototype.format) {
    String.prototype.format = function () {
        var args = arguments;
        return this.replace(/{(\d+)}/g, function (match, number) {
            return typeof args[number] != 'undefined'
                ? args[number]
                : match
                ;
        });
    };
}

var notesAdapter = createAdapter(function (note) {
    var str = `
            <div class="panel-heading">{0}</div>
            <div class="panel-body element">{1}</div>
        `;
    return str.format(note['NoteData']['title'], note['timestamp']);
}, null);

var notesLayoutManager = createLayoutManager(`
        <div class="col-xs-4" role="button">
            <div class="panel panel-default element"></div>
        </div>
    `);

var NotesListView = function ($notesPlaceholder) {
    var selectedNotes = new HashSet(function (val) {
        return val.id;
    });

    var onSelect = function (note, e) {
        var $elem = $(e.delegateTarget).children(".panel");

        if (selectedNotes.contains(note)) {
            if (selectedNotes.size() == 1) {
                $("#next").attr("disabled", "");
            }
            selectedNotes.remove(note);
            $elem.removeClass("panel-success").addClass("panel-default");
        } else {
            if (selectedNotes.size() == 0) {
                $("#next").removeAttr("disabled");
            }
            selectedNotes.insert(note);
            $elem.removeClass("panel-default").addClass("panel-success");
        }

    };

    var notesList = $notesPlaceholder.bslistview(notesAdapter, onSelect);

    notesList.setLayoutManager(createLayoutManager(`
        <div class="col-xs-4" role="button">
            <div class="panel panel-default element"></div>
        </div>
    `));


    notesList.getSelected = function () {
        return selectedNotes.toArray();
    };

    return notesList;
};

var SelectedNotesListView = function ($notesPlaceholder, list, connections={}) {
    for (i = 0; i < list.length; i++) {
        if(!connections[list[i]['id']])
            connections[list[i]['id']] = [];
    }

    function showDialog(note) {
        var $innerList = $("#inner-list");
        $innerList.html("");
        var notesList = new NotesListView($innerList);
        for (i = 0; i < list.length; i++) {
            notesList.prepend(list[i].id, list[i]);
        }

        for(i=0; i<connections[note.id].length; i++){
            notesList.select(connections[note.id][i].id);
        }

        $("#modal").modal('show');

        $("#save-connections").click(()=> {
            connections[note.id] = notesList.getSelected();
            $("#modal").modal('hide');
        });
    }


    var onSelect = function (note, e) {
        //Show dialog
        showDialog(note);
    };

    var notesList = $notesPlaceholder.bslistview(notesAdapter, onSelect);

    notesList.setLayoutManager(notesLayoutManager);

    for (var i = 0; i < list.length; i++) {
        notesList.prepend(list[i].id, list[i]);
    }


    notesList.getConnections = function () {
        return connections;
    };

    return notesList;
};


var firebaseController = new FirebaseController();

function getNotesPage() {
    var $selector = $("#notes-page");

    var notesPage = new WizardPage();
    var game;
    var notesList;
    notesPage.init = function (_game, nextButton, backButton) {
        $selector.show();
        backButton.hide();

        game = _game;

        notesList = new NotesListView($selector.find("#notes-placeholder"));

        firebaseController.getNotes(function (id, note) {
            notesList.prepend(id, note);
        });

        var selectedNotes = game.getNotes();
        for (var i = 0; i < selectedNotes.length; i++) {
            notesList.select(selectedNotes[i].id);
        }
    };

    notesPage.onNextButtonClick = function () {
        $selector.hide();
        game.setNotes(notesList.getSelected());
    };

    notesPage.onBackButtonClick = function () {
        $selector.hide();
    };

    return notesPage;
}

function getConnectionsPage() {
    var $selector = $("#connections-page");
    var connectionsPage = new WizardPage();
    var selectedNotesList;
    var game;
    connectionsPage.init = function (_game, nextButton, backButton) {
        $selector.show();
        game = _game;
        selectedNotesList = new SelectedNotesListView($selector.find("#selected-notes-placeholder"), game.getNotes(), game.getConnections());
    };

    connectionsPage.onNextButtonClick = function () {
        $selector.hide();
        game.setConnections(selectedNotesList.getConnections());
    };

    connectionsPage.onBackButtonClick = function () {
        $selector.hide();
    };

    return connectionsPage;
}

function getStartNotesPage() {
    var $selector = $("#notes-page");

    var startNotesPage = new WizardPage();
    var game;
    var notesList;
    startNotesPage.init = function (_game, nextButton, backButton) {
        $selector.show();
        game = _game;

        notesList = new NotesListView($selector.find("#notes-placeholder"));

        var selectedNotes = game.getNotes();
        for (var i = 0; i < selectedNotes.length; i++) {
            notesList.prepend(selectedNotes[i].id, selectedNotes[i]);
        }

        var startNotes = game.getStartNotes();
        for (i = 0; i < startNotes.length; i++) {
            notesList.select(startNotes[i].id);
        }
    };

    startNotesPage.onNextButtonClick = function () {
        $selector.hide();
        game.setStartNotes(notesList.getSelected());
    };

    startNotesPage.onBackButtonClick = function () {
        $selector.hide();
    };

    return startNotesPage;
}

function getSaveGamePage() {
    var $selector = $("#save-page");
    var saveGamePage = new WizardPage();
    var game;
    var nextButton;
    saveGamePage.init = function (_game, _nextButton, backButton) {
        $selector.show();
        game = _game;
        nextButton = _nextButton;
        nextButton.html("Save");
    };

    saveGamePage.onNextButtonClick = function () {
        game.setName($("#name").val() || "Untitled");
        firebaseController.saveGame(game)
    };

    saveGamePage.onBackButtonClick = function () {
        $selector.hide();
        nextButton.html("Next");
    };

    return saveGamePage;
}


$(document).ready(()=> {
    var wizard = new Wizard($("#next"), $("#back"));

    wizard.addStep(getNotesPage());
    wizard.addStep(getConnectionsPage());
    wizard.addStep(getStartNotesPage());
    wizard.addStep(getSaveGamePage());
    wizard.start();
});




