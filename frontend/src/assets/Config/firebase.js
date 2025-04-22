
import {initializeApp} from 'firebase/app';
import {getAuth} from 'firebase/auth';





const firebaseConfig = {
    apiKey: "AIzaSyC9dhfD0iOL6ed_KCUmqXx_VUouRvMDQZo",
    authDomain: "resumeadjuster123.firebaseapp.com",
    projectId: "resumeadjuster123",
    storageBucket: "resumeadjuster123.firebasestorage.app",
    messagingSenderId: "305698650713",
    appId: "1:305698650713:web:89498fa1174490314a8f48",
    measurementId: "G-EJ909VBQ5X"
};

const app=initializeApp(firebaseConfig);
const auth=getAuth(app);

export{auth ,app};

