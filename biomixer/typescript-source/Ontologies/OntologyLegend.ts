///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="Menu" />

import Menu = require("../Menu");

export class OntologyLegend {

    private menu: Menu.Menu;

    constructor(menu: Menu.Menu){
        this.menu = menu;
    }
    
    initialize(){
        var legend = $("<div>").attr("id", "legend").attr("class", "legend");
        $(this.menu.getMenuBarSelector()).append(legend);

        legend.append($("<p>").text("Outer circle represents the number of concepts in that ontology."));
        legend.append($("<p>").text("Inner circle represents concepts mapped to the central ontology."));
    }

}