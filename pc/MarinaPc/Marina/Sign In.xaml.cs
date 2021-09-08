using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using Firebase.Database;
using Firebase.Database.Query;
using Firebase.Database.Streaming;
using Firebase.Auth;


namespace Marina
{
    /// <summary>
    /// Interaction logic for Sign_In.xaml
    /// </summary>
    public partial class Sign_In : Window
    {
        public const string FirebaseAppUri = "https://marina-a2458.firebaseio.com";
        public const string FirebaseAppKey = "AIzaSyD7wew7GJT9ewLUtXwMeagt7PIW-FamFEo";
        App global = Application.Current as App;


        public Sign_In()
        {
            this.Width = System.Windows.SystemParameters.PrimaryScreenHeight * 0.50;
            this.Height = System.Windows.SystemParameters.PrimaryScreenWidth * 0.35;

            InitializeComponent();
        }

        private void SignIn(object sender, RoutedEventArgs e){
            string Username = UsernameBox.Text;
            string Password = PasswordBox.Text;
            if(Username != "" && Password != "")
            {
                Task t = Task.Run(async () => await ConnectToFirebase(Username, Password));
            }
        }

        private async Task ConnectToFirebase(string username, string password)
        {

            var authProvider = new FirebaseAuthProvider(new FirebaseConfig(FirebaseAppKey));
            global._FirebaseAuth = await authProvider.SignInWithEmailAndPasswordAsync(username, password);

            global._FirebaseClient = new FirebaseClient(FirebaseAppUri,
            new FirebaseOptions
            {
                AuthTokenAsyncFactory = () => Task.FromResult(global._FirebaseAuth.FirebaseToken)
            });


            MainWindow w = new MainWindow();
            await Application.Current.Dispatcher.BeginInvoke((Action)(() => w.Show()));
            await Application.Current.Dispatcher.BeginInvoke((Action)(() => Close()));
            
        }

        private void CloseWindow(object sender, RoutedEventArgs e)
        {
            MainWindow w = new MainWindow();
            w.Show();
            this.Close();
        }

        // make the app draggable
        private void dragging(object sender, MouseButtonEventArgs e)
        {
            if (e.ChangedButton == MouseButton.Left)
            {
                this.DragMove();
            }
        }
    }
}
