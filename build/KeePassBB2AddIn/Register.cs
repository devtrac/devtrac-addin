using System;
using System.Collections.Generic;
using System.Text;
using System.Resources;
using Microsoft.Win32;
using System.Runtime.InteropServices;
using System.Reflection;

namespace KeePassBB2AddIn
{
    public class Register
    {

        public static Assembly me = Assembly.GetExecutingAssembly();

        public static string CompanyString = ((AssemblyCompanyAttribute)me.GetCustomAttributes(
            typeof(AssemblyCompanyAttribute), false)[0]).Company;

        public static string DescriptionString = ((AssemblyDescriptionAttribute)me.GetCustomAttributes(
            typeof(AssemblyDescriptionAttribute), false)[0]).Description;

        public static string ProductString = ((AssemblyProductAttribute)me.GetCustomAttributes(
            typeof(AssemblyProductAttribute), false)[0]).Product;

        public static String VersionString = me.GetName().Version.ToString();

        public const String GUID = "442A9104-A979-420e-A0E5-9466DB6D737E";
        public static String getGUID()
        {
            return GUID;
        }

        [ComRegisterFunctionAttribute]
        public static void register(String t)
        {
            RegistryKey baseKey = null;
            try
            {
                baseKey = Registry.ClassesRoot.CreateSubKey(@"CLSID\{" + getGUID() + "}");
                if (baseKey != null)
                {
                    baseKey.CreateSubKey(@"Implemented Categories\{DFCE97AB-25ED-4335-BB00-FE5863F41DED}");
                }
            }
            catch { }
            finally
            {
                if (baseKey != null)
                {
                    baseKey.Close();
                }
            }
        }

        // Here we cleanup any registry values left from Regasm /u.
        [ComUnregisterFunction(), ComVisible(false)]
        public static void UnregisterServer(Type t)
        {
            try
            {
                Registry.ClassesRoot.DeleteSubKeyTree(@"CLSID\{" + getGUID() + "}");
            }
            catch { }
        }

    }
}
