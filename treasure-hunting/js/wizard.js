/**
 * Created by Xato on 9/15/2016.
 */

function Game(){
    var name = "Untitled";
    var notes = [];
    var connections = {};
    var startNotes = [];
    this.getNotes = function(){
        return notes;
    };

    this.setNotes = function(_notes){
        notes = _notes;
    };

    this.getConnections = function(){
        return connections;
    };

    this.getStartNotes = function(){
        return startNotes;
    };

    this.setStartNotes = function(_startNotes){
        startNotes = _startNotes
    };

    this.setConnections = function(_connections){
        connections = _connections;
    };

    this.setName = function(_name){
        this.name = _name;
    };

    this.getName = function(){
        return this.name;
    }
}

function WizardPage(){

    this.init = function(game, nextButton, backButton){

    };

    this.onNextButtonClick = function(){

    };

    this.onBackButtonClick = function(){

    };
}


function Wizard(nextButton, backButton){
    var steps = [];
    var currentStep = 0;
    var game = new Game();

    this.addStep = function(wizardPage){
        steps.push(wizardPage);
    };

    this.start = function(){
        steps[currentStep].init(game, nextButton, backButton);
    };

    function resetButtons(){
        nextButton.show();
        backButton.show();
        nextButton.removeAttr("disabled");
        backButton.removeAttr("disabled");
    }

    nextButton.click(()=>{
        resetButtons();
        if(currentStep < steps.length)
            steps[currentStep++].onNextButtonClick();

        if(currentStep < steps.length)
            steps[currentStep].init(game, nextButton, backButton);
    });

    backButton.click(()=>{
        resetButtons();
        if(currentStep >= 0)
            steps[currentStep--].onBackButtonClick();

        if(currentStep >= 0)
            steps[currentStep].init(game, nextButton, backButton);
    });
}