import logo from "./assets/images/White-Warhammerlogo.webp";
import "./App.css";
import "./assets/css/style.css";
import FileUploader from "./components/FileUploader";

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <div className="main-section">
          <p>Instructions:</p>
          <p>1. Download HTML file from battlescribe</p>
          <p>2. Upload your HTML file to which style you prefer</p>
          <FileUploader />
        </div>
      </header>
      <div className="footer">
      <footer ><p >Proudly made by <a className="footer-a" href="https://jonahskinner.com/">Jonah Skinner</a></p></footer>
      </div>
    </div>
  );
}

export default App;
