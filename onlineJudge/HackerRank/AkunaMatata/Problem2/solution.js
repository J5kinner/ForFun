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
 * Complete the 'closestNumbers' function below.
 *
 * The function accepts INTEGER_ARRAY numbers as parameter.
 */

function closestNumbers(numbers) {
    // Write your code here
    
    const sorted = numbers.sort();
    let minimum = 0; 
    for(let j=0; j<sorted.length; j++){
        
    }
    
    for(let i=0; i<sorted.length; i++){
        if(sorted[i] !== undefined && sorted[i+1] !== undefined )
        console.log(sorted[i] + " " + sorted[i+1]);
    }

}
function main() {
    const numbersCount = parseInt(readLine().trim(), 10);

    let numbers = [];

    for (let i = 0; i < numbersCount; i++) {
        const numbersItem = parseInt(readLine().trim(), 10);
        numbers.push(numbersItem);
    }

    closestNumbers(numbers);
}
