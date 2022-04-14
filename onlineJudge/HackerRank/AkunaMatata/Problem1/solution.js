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
 * Complete the 'compareStrings' function below.
 *
 * The function is expected to return an INTEGER.
 * The function accepts following parameters:
 *  1. STRING s1
 *  2. STRING s2
 */

function compareStrings(s1, s2) {
    // Write your code here
    let arr1 = s1.split('');
    let arr2 = s2.split('');
    let hashtags = arr1.filter(x => x === '#');
    console.log(hashtags.length);
    let hashtags2 = arr2.filter(y => y === '#');
    console.log(hashtags.length);
    

 
    for(let i=0; i<=arr1.length; i++){
        if(arr1[i] === '#') {
            if(arr1[i+1] === '#'){
                i--;
            }
            arr1.splice(i, 1);
            arr1.splice(i-1, 1);
            i--;
        }
    }

    for(let j=0; j<=arr2.length; j++){
        if(arr2[j] === '#') {
            if(arr2[j+1] === '#'){
                j--;
            }
            arr2.splice(j, 1);
            arr2.splice(j-1, 1);
            j--;
            
            // console.log(arr2 + " j: " + j);

        }
    }

    for(let x=0; x<arr1.length; x++){
        if(arr1[x] !== arr2[x]){
            return 0;
        }
    }
    return 1;

 


    
    
    

}

function main() {
    const ws = fs.createWriteStream(process.env.OUTPUT_PATH);

    const s1 = readLine();

    const s2 = readLine();

    const result = compareStrings(s1, s2);

    ws.write(result + '\n');

    ws.end();
}
