
    /**
     * Returns the average of all contiguous sub-arrays
     * of size k in the given integer array, starting with
     * the first element.
     *
     * @param {Array<number>} arr Integer array.
     * @param {number} k Number of contiguous elements in the sub-array.
     * @return {Array<number>} Array of averages.
     */
    const averages = (arr, k) => {
      //1 based index? 
     
      //multiple cases of different values with the same size 4 < 5
     // [1, 2, 3, 4], 3
     
    // [{1, {2, 3}, 4}], 3
      
      //when k==1 return each value of the array as floats .tofixed()
      let total = 0.0;
      let newArr = new Array();
      let curStart = 0;

      for(let i = 0; i < arr.length; i++){
    
          total += arr[i];
       
            if(i - curStart + 1 === k){
             
              newArr.push(total/k);
              total = newArr[curStart++];
          } 
          
        
      }
      console.log(newArr);
    
    }
    console.log(averages([1, 3, -2, 6, 4], 1));




