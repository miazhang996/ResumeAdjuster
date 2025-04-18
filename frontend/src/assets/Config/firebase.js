
import {initializeApp} from 'firebase/app';
import {getAuth} from 'firebase/auth';





const firebaseConfig = {
    apiKey: "AIzaSyBJBQi6SHVPhg44WoZ1xpWCSDJODua1Xnk",
    authDomain: "resumeadjuster-8fb0a.firebaseapp.com",
    projectId: "resumeadjuster-8fb0a",
    storageBucket: "resumeadjuster-8fb0a.firebasestorage.app",
    messagingSenderId: "643083669027",
    appId: "1:643083669027:web:5ba0444ec16b12f9d3ab96",
    measurementId: "G-L2ZE6TDPGC"
};

const app=initializeApp(firebaseConfig);
const auth=getAuth(app);

export{auth ,app};

