using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.IO;
using System.Data;
using System.Diagnostics;



namespace Marina.Classes
{
    
    public class Execute
    {
        public static string FeaturesList(Entity query)
        {
            return "everything you can do i can do better";
        }
        
        public static string Calculate(Entity query)
        {
            if(query.getType() == "Expression")
            {
                DataTable dt = new DataTable();
                try
                {
                    var answer = dt.Compute(query.getValue(), null).ToString();
                    return answer;
                }
                catch (Exception e)
                {
                    return e.ToString();    
                }
                
            }
            return "not a valid expression";
        }

        public static string search(Entity query)
        {
            System.Diagnostics.Process.Start(@"www.google.com/search?q=" + query.getValue());
            return "Searching for " + query.getValue();
        }

        public static string showTime(Entity entity)
        {
            return "The time is " + DateTime.Now.ToString("hh:mm tt");

        }

        public static string Weather(Entity date)
        {
            return "the weather " + date.getValue() + " is: ...";
        }

        /*
        // open programs and play music
        */
        public static string opener(Entity search)
        {
            string res = "";
            List<string> files = new List<string>();
            files = findProgram(search.getValue());
            if (files.Count == 0)
            {
                return "not found";
            }
            res = files[0];
            run(files[0]);

            return "opening " + Path.GetFileNameWithoutExtension(files[0]);

        }

        public static string PlaySong(Entity search)
        {
            string res = "";
            List<string> songs = new List<string>();
            songs = findMusic(search.getValue());
            if (songs.Count == 0)
            {
                return "not found";
            }
            res = songs[0];
            run(songs[0]);

            return "playing " + Path.GetFileNameWithoutExtension(songs[0]);

        }

        //
        public static List<string> findMusic(string search)
        {
            string sDir = Environment.GetFolderPath(Environment.SpecialFolder.MyMusic);
            List<string> files = new List<string>(Directory.GetFiles(sDir, "*" + search + "*.mp3", SearchOption.AllDirectories));
            return files;
        }

        public static List<string> findProgram(string search)
        {
            string sDir = "C:\\ProgramData\\Microsoft\\Windows\\Start Menu\\Programs";
            List<string> files = new List<string>(Directory.GetFiles(sDir, "*" + search + "*.lnk", SearchOption.AllDirectories));
            return files;
        }

        public static void run(string path)
        {
            StreamWriter w = new StreamWriter(Path.Combine(Directory.GetCurrentDirectory(), "file.bat"));
            w.WriteLine("start \"\" \"" + path + "\"");
            w.Close();
            Process p = new Process();
            p.StartInfo.CreateNoWindow = true;
            p.StartInfo.RedirectStandardOutput = false;
            p.StartInfo.UseShellExecute = false;
            p.StartInfo.FileName = Path.Combine(Directory.GetCurrentDirectory(), "file.bat");
            p.Start();
            p.WaitForExit();
        }
    }
}
