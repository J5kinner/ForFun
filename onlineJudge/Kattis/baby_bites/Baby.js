const readline = require('readline');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});
let first = true;
let allMumble = true;
let fishy = false;
rl.on('line', (line) => {
    if(first) {
        n = +line;
        first = false;
    }
    var array = line.split(' ');
    for(let i = 0; i<n; i++) {
        if(array[i] === "mumble"){
            if(!allMumble && !array[0] && !array[array.length]){
                if((array[i-1] - array[i+1]) > 2){
                    fishy = true;
                }

            }
            
            console.log("mumble detected");
        }
        if(array[i] != "mumble") {
            allMumble = false;
        } 
        // console.log(array[i]);
    }
    if(allMumble) {
        console.log("makes sense")
    } else if(fishy) {
        console.log("something is fishy");
    } else {
        console.log("makes sense");
    }
});
