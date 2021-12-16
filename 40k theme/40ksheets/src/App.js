import logo from "./assets/images/White-Warhammerlogo.png";
import "./App.css";
import "./assets/css/style.css";
import FileUploader from "./components/FileUploader";

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <div className="main-section">
          <FileUploader />
        </div>
      </header>
    </div>
  );
}

export default App;
