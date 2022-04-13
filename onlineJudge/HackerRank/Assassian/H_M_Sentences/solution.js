isAnagram = (word1, word2) => {
    if( word1 === word2) {
        return true;
    } else {
        return false;
    }
}
const countSents = (wordSet, sentence) => {
    const anagramMap = new Map();
    let totalAnagrams = 0;
        for(let i=0; i<wordSet.length; i++){ //O(n)
            const sort1 = wordSet[i].split('').sort((a,b) => a.localeCompare(b)).join(''); //O(nlogn)
        
            if(anagramMap.has(sort1)){ //O(1)
                const value = anagramMap.get(sort1);
                anagramMap.set(sort1, value+1);
            } else {
                anagramMap.set(sort1, 1);
            }
        }
        const splSent = sentence.split(" ");
        // console.log(anagramMap);
        for(let j=0; j<splSent.length; j++){ //O(n)
            const sortSentWord = splSent[j].split('').sort((a,b) => a.localeCompare(b)).join(''); //O(nlogn)
            // console.log("word "+ splSent[j] + " has: " + anagramMap.has(sortSentWord) + " times " + anagramMap.get(sortSentWord))
                if(anagramMap.has(sortSentWord) && (anagramMap.get(sortSentWord) > 1)){ //O(1)
                    totalAnagrams += anagramMap.get(sortSentWord);
                }
        }

        
    return totalAnagrams;
}
const wordSet = ["listen", "silent", "it", "is", "funeral", "realfun", "santa", "satan", "was", "natas"];
console.log(countSents(wordSet, "listen it was satan natas is silent santa funeral realfun"));