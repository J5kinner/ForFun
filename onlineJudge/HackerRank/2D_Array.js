'use strict';

const fs = require('fs');

process.stdin.resume();
process.stdin.setEncoding('utf-8');

let inputString = '';
let currentLine = 0;

process.stdin.on('data', function (inputStdin) {
    inputString += inputStdin;
});

process.stdin.on('end', function () {
    inputString = inputString.split('\n');

    main();
});

function readLine() {
    return inputString[currentLine++];
}

/*
 * Complete the 'hourglassSum' function below.
 *
 * The function is expected to return an INTEGER.
 * The function accepts 2D_INTEGER_ARRAY arr as parameter.
 */

function hourglassSum(arr) {
    // Write your code here
    let bigSum;
    let hourGlassTotal = 0;
    let x = 0;
    let y = 0;

    while (x < 4) {
       
        hourGlassTotal = arr[x][y] + arr[x][y + 1] + arr[x][y + 2] + arr[x + 1][y + 1] + arr[x + 2][y] + arr[x + 2][y + 1] + arr[x + 2][y + 2];
         if(x === 0 && y === 0){
            bigSum = hourGlassTotal
        }

        console.log(arr[x][y] + " " + arr[x][y + 1] + " " + arr[x][y + 2] + " " + arr[x + 1][y + 1] + " " + arr[x + 2][y] + " " + arr[x + 2][y + 1] + " " + arr[x + 2][y + 2] + " Total: " + (arr[x][y] + arr[x][y + 1] + arr[x][y + 2] + arr[x + 1][y + 1] + arr[x + 2][y] + arr[x + 2][y + 1] + arr[x + 2][y + 2]));
        
        if (hourGlassTotal > bigSum) {
            bigSum = hourGlassTotal;
        }
        if (y === 3) {
            y = 0;
            x++;
        } else {
            y++;
        }
        

    }

    return bigSum;
}

function main() {
    const ws = fs.createWriteStream(process.env.OUTPUT_PATH);

    let arr = Array(6);

    for (let i = 0; i < 6; i++) {
        arr[i] = readLine().replace(/\s+$/g, '').split(' ').map(arrTemp => parseInt(arrTemp, 10));
    }

    const result = hourglassSum(arr);

    ws.write(result + '\n');

    ws.end();
}
