export class Menu {

    static defaultMenuName = "Menu";
    static menuArrowPrefix = "<< ";
    
    private menuSelector: string = 'div#hoveringGraphMenu';
    
    initializeMenu(menuName: string = Menu.defaultMenuName){
        // Append the pop-out panel. It will stay hidden except when moused over.
        var trigger = $("<div>").attr("id", "trigger");
        $("#chart").append(trigger);
        trigger.append($("<p>").text(Menu.menuArrowPrefix+menuName)); // "<< Menu" by default
        trigger.append($("<div>").attr("id", "hoveringGraphMenu"));
        
        $('#trigger').hover(
                (e) => {
                    $(this.menuSelector).show(); //.css('top', e.pageY).css('left', e.pageX);
                     // Looks bad when it's not fully visible, due to children inheriting transparency
                    $(this.menuSelector).fadeTo(0, 1.0);
                },
                function() {
                //  $(menuSelector).hide();
                }
        );
        
    }
    
    closeMenuLambda(){
        return ()=>{
            $(this.menuSelector).hide()
        };
    }
    
    getMenuSelector(){
        return this.menuSelector;
    }
    
}