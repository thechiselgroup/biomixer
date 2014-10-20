export class Menu {

    static defaultMenuName = "Menu";
    static menuClosedPrefix = "Show ";
    static menuOpenPrefix = "Hide ";
    
    static menuLabelClass = "menuLabel";
    
    static menuItemExpanderLabelClass = "menuLabelExpander";
    
    private menuSelector: string = 'div#hoveringGraphMenu';
    
    private menuBarSelector: string = "#top_menu_bar";
    
    private menuName: string = "";
    
    initializeMenu(menuName: string = Menu.defaultMenuName){
        this.menuName = menuName;
        // Append the pop-out panel. It will stay hidden except when moused over.
        var trigger = $("<div>").attr("id", "menuTriggerContainer");
//        $("#chart").append(trigger);
         $(this.menuBarSelector).append(trigger);
        trigger.append($("<p>").attr("id", "trigger")
        .addClass("unselectable")
            .text(Menu.menuClosedPrefix+menuName)); // "<< Menu" by default
        trigger.append($("<div>").attr("id", "hoveringGraphMenu"));
        
        // Opted for click control only
        //$('#trigger').hover(
        //        (e) => {
        //            $(this.menuSelector).show(); //.css('top', e.pageY).css('left', e.pageX);
        //             // Looks bad when it's not fully visible, due to children inheriting transparency
        //            $(this.menuSelector).fadeTo(0, 1.0);
        //        },
        //        function() {
        //        //  $(menuSelector).hide();
        //        }
        //);
        var outerThis = this;
        $('#trigger').click(
            (event)=>{
            	event.stopPropagation();
                $(this.menuSelector).slideToggle({ complete: ()=>{outerThis.updateMenuText();} });
            }
        );
        
    }
    
    updateMenuText(){
        if($(this.menuSelector).css("display") === "none"){
            $('#trigger').text(Menu.menuClosedPrefix+this.menuName);
        } else {
            $('#trigger').text(Menu.menuOpenPrefix+this.menuName);
        }
    }
    
    closeMenuLambda(){
        return ()=>{
            $(this.menuSelector).slideUp();
        };
    }
    
    getMenuSelector(){
        return this.menuSelector;
    }
    
    getMenuBarSelector(){
        return this.menuBarSelector;   
    }
    
}