import React from 'react'
import FileUploader from './FileUploader'
import Preview from './Preview'

function Uploader() {
    return (
        <div className="uploader-container">
            <Preview/>
            <FileUploader/>
            
        </div>
    )
}

export default Uploader
