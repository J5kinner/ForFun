import React, {useState} from 'react'

function FileUploader() {
   
    const [selectedFile, setSelectedFile] = useState(null);

    const onChangeHandler = (e) => {
        console.log(e.target.files[0])
        setSelectedFile(e.target.files[0])
    }
    return (
        <div className="upload-container">
            <input type="file" name="file" onChange={onChangeHandler}/>
            
        </div>
    )
}

export default FileUploader
