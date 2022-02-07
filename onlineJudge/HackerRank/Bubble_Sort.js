'use strict';

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
 * Complete the 'countSwaps' function below.
 *
 * The function accepts INTEGER_ARRAY a as parameter.
 */

function countSwaps(a) {
    // Write your code here
    let sortedArray = a;
    let swapCount = 0;
    // console.log("start "+ sortedArray);
    for (let i=0; i<sortedArray.length; i++ ) {
        for(let j=0; j<sortedArray.length-1; j++) {
            if(sortedArray[j] > sortedArray[j + 1]) {
                swapCount++;
                const swapJ = sortedArray[j];
                const swapNext = sortedArray[j+1];
                sortedArray[j] = swapNext;
                sortedArray[j+1] = swapJ;
                // console.log("Swap activated "+ sortedArray);
            }
        }
    }
    const first = sortedArray[0];
    const last = sortedArray[sortedArray.length-1];
    console.log("Array is sorted in " + swapCount + " swaps.");
    
    console.log("First Element: " + first);
    
    console.log("Last Element: " + last);

}



function main() {
    const n = parseInt(readLine().trim(), 10);

    const a = readLine().replace(/\s+$/g, '').split(' ').map(aTemp => parseInt(aTemp, 10));

    countSwaps(a);
}
