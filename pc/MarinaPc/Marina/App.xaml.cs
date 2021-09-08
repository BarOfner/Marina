using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using Firebase.Database;
using Firebase.Database.Query;
using Firebase.Database.Streaming;
using Firebase.Auth;

namespace Marina
{
    /// <summary>
    /// Interaction logic for App.xaml
    /// </summary>
    public partial class App : Application
    {
        public FirebaseClient _FirebaseClient;
        public FirebaseAuthLink _FirebaseAuth;
    }
}
