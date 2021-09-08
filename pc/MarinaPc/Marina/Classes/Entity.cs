using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Marina.Classes
{
    public class Entity
    {
        private string value;

        // type of the entity
        private string type;

        public Entity(string v, string t)
        {
            value = v;
            type = t;
        }

        public string getValue(){
            return value;
        }
    

        public string getType()
        {
            return type;
        }
    }
}
