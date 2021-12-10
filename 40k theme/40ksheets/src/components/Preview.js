import React from "react";
import singleColumnImg from "../assets/images/singleCol.png";
import doubleColumnImg from "../assets/images/doubleCol.png";

function Preview() {
  return (
    <div className="preview-container">
      <div className="single">
        <h1>Single Column</h1>
        <img src={singleColumnImg} alt="singular column example" />
      </div>
      {/* <div className="double">
        <h1>Double Column</h1>
        <img src={doubleColumnImg} alt="double column example" />
      </div> */}
    </div>
  );
}

export default Preview;
