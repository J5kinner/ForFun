import logo from './logo.svg';
import './App.css';
import FileUploader from './components/FileUploader';
import Preview from './components/Preview';
import "./assets/css/style.css";
import Uploader from './components/Uploader';

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <div className="main-section">
          <Uploader/>
        </div>
     
        

        
     
       
      </header>
    </div>
  );
}

export default App;
