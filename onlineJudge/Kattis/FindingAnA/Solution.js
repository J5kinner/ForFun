const readline = require("readline");

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
});

rl.on("line", (line) => {
    const length = line.length();
    const input = line.split(" ");
    // console.log(input[0][0]);
        let findA = false;
        let findStr = "";
        console.log(input);
        console.log(length);
        for(let x=0; x<input.size(); x++) {

            if(input[0][x]=== 'a'){
                findA = true;
            }
            if(findA) {
                findStr += input[0][x];
                // console.log(findStr);
    
            }
            // console.log(input[0][x]);
    
        }
    // console.log("answer " + findStr);
})

