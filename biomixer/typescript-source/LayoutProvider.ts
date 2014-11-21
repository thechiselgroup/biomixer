///<reference path="headers/require.d.ts" />

export interface ILayoutProvider {
    
    getLayoutPositionSnapshot(): {[nodeUri: string]: {x: number; y: number}};
    
    setLayoutFixedCoordinates(layout: {[nodeUri: string]: {x: number; y: number}});
    
    applyNewLayoutLambda(layoutLambda: LayoutRunner);
    
    setNewLayoutWithoutRunning(layoutLambda: LayoutRunner);
    
    /**
     * Special situation relating to things such as import and undo/redo.
     * Other layouts don't need immediate application in this way.
     * Needed to bypass any timers that normally delay application.
     */
    applyFixedLayout();
    
    getLayoutRunner(): LayoutRunner;
    
}

export interface LayoutRunner {
    (refresh?: boolean): void;
};
