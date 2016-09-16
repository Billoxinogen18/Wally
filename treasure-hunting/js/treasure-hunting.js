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

var $gamesList;
var $notesList;

$(document).ready(function () {


    var firebaseController = new FirebaseController();


    initGamesList("#games-list");
    initNotesList("#notes-list");

    firebaseController.getNotes(function (id, note) {
        $notesList.prepend(id, note);
    });


});


function initGamesList(gameListSelector) {
    var adapter = createAdapter(
        function (game) {
            // return resolvedIncidentRawHtml.format();
        }, null);

    var onClick = function (game) {
        var $detailView = $("#details-view");
        $detailView.html("");
    };

    $gamesList = $(gameListSelector).bslistview(adapter, onClick);
}

function initNotesList(notesListSelector) {
    var adapter = createAdapter(
        function (note) {
            console.log(note);
            return "<div><h4>{0}</h4><strong>{1}</strong></div>".format(note['NoteData']['title'], note['NoteData']['note']);
        }, null);

    var onClick = function (note) {
        var $detailView = $("#details-view");
        $detailView.html("");
    };

    $notesList = $(notesListSelector).bslistview(adapter, onClick);
}