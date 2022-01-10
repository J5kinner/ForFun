'use strict';

const fs = require('fs');

process.stdin.resume();
process.stdin.setEncoding('utf-8');

let inputString = '';
let currentLine = 0;

process.stdin.on('data', function(inputStdin) {
    inputString += inputStdin;
});

process.stdin.on('end', function() {
    inputString = inputString.split('\n');

    main();
});

function readLine() {
    return inputString[currentLine++];
}

/*
 * Complete the 'countingValleys' function below.
 *
 * The function is expected to return an INTEGER.
 * The function accepts following parameters:
 *  1. INTEGER steps
 *  2. STRING path
 */

function countingValleys(steps, path) {
    // Write your code here
    let direction = 0;
    let vStart = 0;
    let vEnd = 0;
    for (let i=0; i<steps; i++) {
        if (direction === 0 && path[i] === "D"){ //starting a valley
            vStart+=1;
            console.log("Activated Vstart")
        }
        if (vStart > 0 && direction === 0) {
            vEnd+=1;
            vStart-=1;
            console.log("Activated vEnd")
        }
        if (path[i] === "U") {
            direction++;
        }
        if (path[i] === "D") {
            direction--;
        }
    }
    return vEnd;
}

function main() {
    const ws = fs.createWriteStream(process.env.OUTPUT_PATH);

    const steps = parseInt(readLine().trim(), 10);

    const path = readLine();

    const result = countingValleys(steps, path);

    ws.write(result + '\n');

    ws.end();
}
