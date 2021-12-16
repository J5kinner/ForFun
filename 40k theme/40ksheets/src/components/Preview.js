import React from "react";

function Preview({ title, image }) {
  return (
    <div className="preview-container">
      <div className="single">
        <h1>{title}</h1>
        <img src={image} alt="singular column example" />
      </div>
    </div>
  );
}

export default Preview;
