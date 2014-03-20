///<reference path="headers/require.d.ts" />

export function getTime(){
    var now = new Date();
    return now.getHours()+":"+now.getMinutes()+':'+now.getSeconds();
}