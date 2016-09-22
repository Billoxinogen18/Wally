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

var firebaseController = new FirebaseController();

$(document).ready(function () {

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
    var $editModal = $("#editNoteModal");

    var $form = $editModal.find("#editForm");
    var $idField = $form.find("#note-id");
    var $answerField = $form.find("#answer-field");
    var $noteField = $form.find("#note-field");
    var $titleField = $form.find("#title-field");

    var adapter = createAdapter(
        function (note) {
            console.log(note);
            return "<div><h4>{0}</h4><strong>{1}</strong></div>".format(note['NoteData']['title'], note['NoteData']['note']);
        }, null);

    var onClick = function (note) {
        $editModal.modal('show');
        $idField.val(note.id);
        $answerField.val(note['PuzzleData']['Answers'].join(";"));
        $noteField.val(note['NoteData']['note']);
        $titleField.val(note['NoteData']['title']);
    };

    $notesList = $(notesListSelector).bslistview(adapter, onClick);

    $("#ok-btn").click((e)=>{
        var note = $notesList.get($idField.val());
        note['NoteData']['title'] = $titleField.val();
        note['NoteData']['note'] = $noteField.val();
        note['PuzzleData']['Answers'] = $answerField.val().split(";").filter((i)=>{return i.length > 0});
        $notesList.update($idField.val(), note);
        firebaseController.updateNote(note);
    });
}