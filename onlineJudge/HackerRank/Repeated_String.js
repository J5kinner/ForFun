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
 * Complete the 'repeatedString' function below.
 *
 * The function is expected to return a LONG_INTEGER.
 * The function accepts following parameters:
 *  1. STRING s
 *  2. LONG_INTEGER n
 */

function repeatedString(s, n) {
    // Write your code here
    let length = s.length;
    let mult = Math.floor(n / length);
    let extra = n % length;
    console.log("extra " + extra)
    let count = 0;
    let extraCounts = 0;
    console.log(length + " / " + n + " = " + mult);
    for (let i=0; i<s.length; i++) {
        if (/a/.test(s[i])){
            count++;
        }
    }
    for(let j=0; j<extra; j++){
        if(/a/.test(s[j])){
            extraCounts++;
        }
    }
    console.log(count + " * " + mult + " + " + extraCounts);

    return count * mult + extraCounts;
    
    

}

function main() {
    const ws = fs.createWriteStream(process.env.OUTPUT_PATH);

    const s = readLine();

    const n = parseInt(readLine().trim(), 10);

    const result = repeatedString(s, n);

    ws.write(result + '\n');

    ws.end();
}
