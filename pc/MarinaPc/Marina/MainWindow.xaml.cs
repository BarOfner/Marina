﻿using System;
using System.Diagnostics;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Web;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Data;
using System.Windows.Input;
using System.Runtime.InteropServices;
using System.IO;
using Newtonsoft.Json.Linq;
using Marina.Classes;
using System.Windows.Controls;
using Firebase.Database;
using Firebase.Database.Query;
using Firebase.Database.Streaming;
using Firebase.Auth;


namespace Marina
{
    public partial class MainWindow : Window
    {
        // Firebase Variables
        public const string FirebaseAppUri = "https://marina-a2458.firebaseio.com";
        public const string FirebaseAppKey = "AIzaSyD7wew7GJT9ewLUtXwMeagt7PIW-FamFEo";
        App global = Application.Current as App;


        // Marina app id in LUIS
        const string luisAppId = "badddf4b-7791-445c-8280-9d87e8868389";
        const string subscriptionKey = "86a2dc227c144d59b6d10a9896e4bb8b";
        public List<Label> Messages = new List<Label>();
        // this function makes no sense
        public static async Task<string> MakeRequest(string query)
        {
            HttpClient client = new HttpClient();
            var queryString = HttpUtility.ParseQueryString(string.Empty);

            // insert the subscription key
            client.DefaultRequestHeaders.Add("Ocp-Apim-Subscription-Key", subscriptionKey);

            // insert the query yo the request
            queryString["q"] = query;


            var uri = "https://westus.api.cognitive.microsoft.com/luis/v2.0/apps/" + luisAppId + "?" + queryString;
            var response = await client.GetAsync(uri);


            var strResponseContent = await response.Content.ReadAsStringAsync();

            // return the response
            return strResponseContent.ToString();
        }



        public static string parseReq(string r)
        {
            // create the dictionary of functions
            Dictionary<string, Func<Entity, string>> f = new Dictionary<string, Func<Entity, string>>();
            f.Add("Time.what", Execute.showTime);
            f.Add("Weather", Execute.Weather);
            f.Add("Run", Execute.opener);
            f.Add("Search", Execute.search);
            f.Add("MarinaCanDo", Execute.FeaturesList);
            f.Add("PlaySong", Execute.PlaySong);
            f.Add("Calculate", Execute.Calculate);

            // parse JSON 
            dynamic JsonParsed = JObject.Parse(r);


            string intent = "";
            string entityString = "";
            string typeString = "";
            Entity entity;

            // parse the intent
            try
            {
                intent = JsonParsed.topScoringIntent.intent;
            }
            catch { }

            // parse the entity
            try
            {
                entityString = JsonParsed.entities[0].entity;
                typeString = JsonParsed.entities[0].type;
            }
            catch { }


            /*@TODO
             * add entities array instead of entity 
             * 
             */

            entity = new Entity(entityString, typeString);


            if (f.ContainsKey(intent))
            {
                var res = (f[intent].Invoke(entity));
                return res;
            }
            else
            {
                return "Sorry, I can't understand you";
            }
        }




        private void doRequest(string query)
        {
            // check if the query is empty
            if (query == "")
            {
                return;
            }


            Label queryLabel = new Label();
            queryLabel.Style = (Style)FindResource("request");
            queryLabel.Content = query;
            messages.Children.Add(queryLabel);



            // make connection with LUIS
            var result = Task.Run(async () => { return await MakeRequest(query); }).Result;

            // create new message lable
            Label answer = new Label();
            answer.Style = (Style)FindResource("Answer");
            answer.Content = parseReq(result);
            messages.Children.Add(answer);

            // scroll down
            scrollMessages.ScrollToEnd();
        }


        private void HandleEnterDown(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Enter)
            {
                string query = commandBox.Text;
                doRequest(query);
            }
        }

        private void command(object sender, RoutedEventArgs e)
        {
            string query = commandBox.Text;
            doRequest(query);
        }




        // GUI functions
        public MainWindow()
        {
            this.Width = System.Windows.SystemParameters.PrimaryScreenHeight * 0.50;
            this.Height = System.Windows.SystemParameters.PrimaryScreenWidth * 0.35;

            InitializeComponent();

            
        }

        

        // show and hide
        private void CloseApp(object sender, RoutedEventArgs e)
        {
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

        /*
         * Handle all messages from the server
         */

        

        private void SignInButton(object sender, RoutedEventArgs e)
        {
            Sign_In SignIWindow = new Sign_In();
            SignIWindow.Show();
            this.Close();
        }

        private async void HandleServerRequest(FirebaseEvent<string> data, FirebaseClient Firebase, FirebaseAuthLink auth)
        {

            if (data.EventType == FirebaseEventType.InsertOrUpdate && data.Object.ToLower() != "false")
            {
                await Dispatcher.BeginInvoke((Action)(() => doRequest(data.Object)));
                await Firebase.Child("Users/" + auth.User.LocalId + "/PC/message").PutAsync("false");
            }
        }

        private void StartListen(object sender, RoutedEventArgs e)
        {
            
            if (global._FirebaseAuth != null)
            {
                global._FirebaseClient.Child("Users/" + global._FirebaseAuth.User.LocalId + "/PC")
                .AsObservable<string>()
                .Subscribe(d => HandleServerRequest(d, global._FirebaseClient, global._FirebaseAuth));
            }
            
        }
    }
}
