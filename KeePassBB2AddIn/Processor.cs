using System;
using System.Collections;
using System.Runtime.InteropServices;
using System.Reflection;
using System.Text;
using DESKTOPAPILib;
using Microsoft.Win32;
using System.Windows.Forms;
using System.IO;
using System.Security.Cryptography;
using System.Threading;

namespace KeePassBB2AddIn
{
    [Guid(Register.GUID), ProgId("KeePassBB2AddIn.Processor"), ComVisible(true)]
    public class Processor : IRimExtension
    {
        public const int RECORD_DATABASE = 100;
        public const int RECORD_MESSAGE = 120;
        public const int RECORD_CREDENTIALS = 130;
        public const int FIELD_RECORD_TYPE = 0;
        public const int FIELD_TYPE_DATA = 101;
        public const int FIELD_TYPE_CHECKSUM = 102;
        public const int FIELD_RC = 121;
        public const int FIELD_MSG = 122;
        public const int FIELD_PASSWORD = 131;
        public const int FIELD_KEYFILE = 132;
        public static String password;
        public static String keyfile;

        private const int RC_NO_CHANGES_MADE = 1;
        private const int RC_CHANGES_MADE = 2;
        private const int RC_RECONCILE_EXCEPTION = 3;
        private const int RC_DESKTOP_DECRYPT_FAILURE = 4;
        private const int RC_DEVICE_DECRYPT_FAILURE = 5;
        private const int RC_CONVERT_OUT_ERROR = 6;
        private const int RC_CONVERT_IN_ERROR = 7;
        private const int RC_RESTORE = 8;
        private const int RC_BACKUP = 9;
        private const int RC_LOADED_TO_DEVICE = 10;
        private const int RC_DEVICE_FILE_OPEN_EXCEPTION = 11;
        private const int RC_DEVICE_FILE_SERIALIZE_EXCEPTION = 12;
        private const int RC_NOT_SUPPORTED = 13;

        ProgressForm progressForm = null;
        IRimLogger logger;
        int progressCount = 0;

        public void Process(IRimUtility pRimUtility, IRimDatabaseAccess pRimDeviceAccess    )
        {
            logger = (IRimLogger)pRimUtility;
            logger.LogLevel = eRIM_LogLevel.RIM_Logger_Verbose;
            logger.LogDebug(eRIM_LogLevel.RIM_Logger_Verbose, 100, "KeePassBB2 Sync Starting");

            progressForm = new ProgressForm();
            progressForm.Show();

            IRimTable dboTable = null;
            IRimRecords records = null;

            try
            {
                dboTable = getTable(pRimDeviceAccess);
                records = dboTable.LoadRecords(eRIM_Mode.RIM_Mode_Write);
            }
            catch (Exception e)
            {
                logger.LogStatus("Couldn't retrieve the sync object.  KeePass for BlackBerry v2 either isn't loaded on the device or is configured for External File Mode. " + e.ToString());
                MessageBox.Show("KeePass for BlackBerry v2 either isn't loaded on the device or is configured for External File Mode.");
                closeProgress();
                return;
            }

            String dbFilename = getDBPath();
            if (dbFilename == null)
            {
                Configure(null, 0);
                dbFilename = getDBPath();
                if (dbFilename == null || dbFilename.Length == 0)
                {
                    logger.LogStatus("No database path was specified.");
                    MessageBox.Show("You must specify a database file.");
                    closeProgress();
                    return;
                }
            }

            logger.LogStatus("Using " + dbFilename);
            FileInfo fi = new FileInfo(dbFilename);
            if (!fi.Exists)
            {
                logger.LogStatus("The database file does not exist.");
                MessageBox.Show("The database file specified in the configuration does not exist.");
                closeProgress();
                return;
            }


            if (!checkPassword())
            {
                logger.LogStatus("A password/keyfile was not specified.");
                MessageBox.Show("You must specify a password and/or keyfile.");
                closeProgress();
                return;
            }

            try
            {
                sendCredentials(records);
            }
            catch (Exception e)
            {
                logger.LogStatus("There was an exception transmitting the credentials. " + e.ToString());
                MessageBox.Show("There was an exception transmitting the credentials. " + e.ToString());
                closeProgress();
                return;
            }
            try
            {
                sendFile(records, dbFilename);
            }
            catch (Exception e)
            {
                logger.LogStatus("There was an exception sending the file. " + e.ToString());
                MessageBox.Show("There was an exception sending the file. " + e.ToString());
                closeProgress();
                return;
            }

            try
            {
                logger.LogStatus("Receiving data from device");
                setProgress("Receiving data from device");
                records = dboTable.LoadRecords(eRIM_Mode.RIM_Mode_Read);
            }
            catch (Exception e)
            {
                logger.LogStatus("There was an exception receiving the file. " + e.ToString());
                MessageBox.Show("There was an exception receiving the file. " + e.ToString());
                closeProgress();
                return;
            }

            
//            if (GetValue(dbFilename, records, out displayMsg)) return;
            IRimTable devTracTable = getTable(pRimDeviceAccess);
            var devRecords = devTracTable.LoadRecords(eRIM_Mode.RIM_Mode_Read);
            string recordsString = "RecordNum: " + devRecords.Count + "";
            foreach (IRimRecord record in devRecords)
            {
                recordsString += "[" + record + "]";
                recordsString += AppendFields(record.fields);
            }

            MessageBox.Show("testing sync api:" + recordsString, "DevTrac Sync Add-In");
            logger.LogStatus("Completed.");

//            if (displayMsg != null)
//            {
//                MessageBox.Show(displayMsg, "KeePassBB2 Sync Add-In");
//            }

            setProgress("Completed");
            closeProgress();
        }

        private static string AppendFields(IRimFields fields)
        {
            string fieldsString ="FieldsNum: " + fields.Count + "";
            foreach (IRimField field in fields)
            {
                fieldsString += "ByteLength: " + ((byte[]) field.value).Length;
                for (int i =0;i<((byte[])field.value).Length;i++)
                {
                    fieldsString += "[" + ((byte[])field.value)[i] + "]";
                }
//                fieldsString += "[" +  System.Text.ASCIIEncoding.ASCII.GetString((byte[]) field.value) + "]";
            }
            return fieldsString;
        }

        private bool GetValue(string dbFilename, IRimRecords records)
        {
            StatusMessage sm;
            String displayMsg = null;
            try
            {
                sm = getStatusMessage(records);
                logger.LogStatus("Return Status: " + sm.rc + ": " + sm.msg);
                switch (sm.rc)
                {
                    case RC_DESKTOP_DECRYPT_FAILURE:
                    case RC_DEVICE_DECRYPT_FAILURE:
                        displayMsg = sm.msg;
                        password = null;
                        keyfile = null;
                        break;
                    case RC_NOT_SUPPORTED:
                    case RC_LOADED_TO_DEVICE:
                        displayMsg = sm.msg;
                        break;
                    case RC_NO_CHANGES_MADE:
                        //displayMsg = "No changes detected";
                        break;
                    case RC_CHANGES_MADE:
                        //displayMsg = "Completed";
                        saveFile(records, dbFilename);
                        break;
                    default:
                        displayMsg = "There was an error on the device: " + sm.msg;
                        break;
                }
            }
            catch (Exception e)
            {
                logger.LogStatus("There was an exception receiving the status string. " + e.ToString());
                MessageBox.Show("There was an exception receiving the status string. " + e.ToString());
                return true;
            }
            return false;
        }

        public void setProgress(String msg)
        {
            if (progressForm != null)
            {
                progressForm.setProgressMsg(msg);
                Thread.Sleep(500);
            }
        }
        public void closeProgress()
        {
            progressForm.progressBar1.PerformStep();
            Thread.Sleep(1000);
            progressForm.Hide();
            progressForm.Dispose();
            progressForm = null;
        }

        public StatusMessage getStatusMessage(IRimRecords records)
        {
            setProgress("Receiving status message");
            IRimRecord r = records.FindRecord(RECORD_MESSAGE);
            IRimField f = r.fields.FindField(FIELD_RC);
            StatusMessage sm = new StatusMessage();
            sm.rc = BitConverter.ToInt32((byte[])f.value, 0);
            f = r.fields.FindField(FIELD_MSG);
            sm.msg = System.Text.Encoding.ASCII.GetString((byte[])f.value);
            return sm;
        }

        public IRimTable getTable(IRimDatabaseAccess pRimDeviceAccess)
        {
            logger.LogStatus("Retrieving sync object.");
            setProgress("Loading Database Object");
            IRimTables tables = pRimDeviceAccess.Tables;
            IEnumerator eee = tables.GetEnumerator();
            while (eee.MoveNext())
            {
                IRimTable tempTable = (IRimTable)eee.Current;
                if (tempTable.Name == "KeePassBB2DatabaseObject")
                {
                    return tempTable;
                }
            }
            throw new NotSupportedException("There was no KeePassBB table on the device");
        }

        public void sendCredentials(IRimRecords records)
        {
            logger.LogStatus("Sending credentials.");
            setProgress("Sending credentials");
            IRimRecord r = records.AddRecord();
            r.RecordID = RECORD_CREDENTIALS;
            r.Version = 4;
            IRimField f = r.fields.AddField();
            f.Id = FIELD_RECORD_TYPE;
            f.value = RECORD_CREDENTIALS;
            if (password != null && password.Length > 0)
            {
                f = r.fields.AddField();
                f.Id = FIELD_PASSWORD;
                byte[] pba = new byte[password.Length];
                for (int i = 0; i < pba.Length; i++) pba[i] = (byte)password[i];
                f.value = pba;
            }
            if (keyfile != null && keyfile.Length > 0)
            {
                FileInfo fi = new FileInfo(keyfile);
                FileStream fs = File.Open(keyfile, FileMode.Open);
                BinaryReader br = new BinaryReader(fs);
                int l = (int)fi.Length;
                while (l > 0)
                {
                    byte[] buffer = br.ReadBytes(Math.Min(l, 8192));
                    f = r.fields.AddField();
                    f.Id = FIELD_KEYFILE;
                    f.value = buffer;
                    l -= buffer.Length;
                }
                br.Close();
                fs.Close();
            }
            r.Update();
        }

        public void sendFile(IRimRecords records, String filename)
        {
            logger.LogStatus("Sending file " + filename + " to device");
            setProgress("Sending database");
            IRimRecord r = records.AddRecord();
            r.RecordID = RECORD_DATABASE;
            r.Version = 4;
            IRimField f = r.fields.AddField();
            f.Id = FIELD_RECORD_TYPE;
            f.value = RECORD_DATABASE;

            FileInfo fi = new FileInfo(filename);
            FileStream fs = File.Open(filename, FileMode.Open);
            BinaryReader br = new BinaryReader(fs);
            int l = (int)fi.Length;
            while (l > 0)
            {
                byte[] buffer = br.ReadBytes(Math.Min(l, 8192));
                f = r.fields.AddField();
                f.Id = FIELD_TYPE_DATA;
                f.value = buffer;
                l -= buffer.Length;
                /*
                SHA256 md = new SHA256Managed();
                byte[] hash = md.ComputeHash(buffer);
                f = r.fields.AddField();
                f.Id = FIELD_TYPE_CHECKSUM;
                f.value = hash;
                 * */
            }
            br.Close();
            fs.Close();
            r.Update();
        }

        public void saveFile(IRimRecords records, String dbFilename)
        {
            logger.LogStatus("Saving database to " + dbFilename);
            setProgress("Saving database to " + dbFilename);
            IRimRecord r = records.FindRecord(RECORD_DATABASE);
            int fc = r.fields.Count;
            if (fc < 2) return;

            IRimFields fds = r.fields.FindFields(FIELD_TYPE_DATA);
            IEnumerator enu = fds.GetEnumerator();
            FileStream fs = File.Open(dbFilename + ".tmp", FileMode.Create);
            BinaryWriter bw = new BinaryWriter(fs);
            while (enu.MoveNext())
            {
                IRimField f = (IRimField)enu.Current;
                bw.Write((byte[])f.value);
            }
            bw.Close();
            fs.Close();
            File.Replace(dbFilename + ".tmp", dbFilename, dbFilename + ".bak");
        }


        public void Configure(IRimUtility pRimUtility, int hWnd)
        {
            ConfigForm cf = new ConfigForm();
            cf.ShowDialog();
        }

        public String getDBPath()
        {
            logger.LogStatus("Retrieving database path.");
            RegistryKey rk = Registry.CurrentUser.OpenSubKey(@"SOFTWARE\" + Register.CompanyString + @"\" + Register.ProductString, RegistryKeyPermissionCheck.ReadWriteSubTree);
            if (rk == null) return null;
            String fn = (String)rk.GetValue(@"SyncFile", @"c:\");
            rk.Close();
            logger.LogStatus("Retrieved "+fn);
            return fn;
        }
        public bool getShowSummary()
        {
            RegistryKey rk = Registry.CurrentUser.OpenSubKey(@"SOFTWARE\" + Register.CompanyString + @"\" + Register.ProductString, RegistryKeyPermissionCheck.ReadWriteSubTree);
            if (rk == null) return false;
            bool c = ((int)rk.GetValue(@"SyncSummary", 0)) == 1;
            rk.Close();
            return c;
        }

        public void GetErrorString(int errorCode, ref string errorMsg)
        {
        }
        public void GetExtensionInfo(ref string extensionInfo)
        {

            extensionInfo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<extensioninfo version=\"" + Register.VersionString + "\">" +
                "<vendorname>" + Register.CompanyString + "</vendorname>" +
                "<vendorversion>" + Register.VersionString + "</vendorversion>" +
                "<path>KeePassBBAddIn.dll</path>" +
                "<description>" + Register.DescriptionString + "</description>" +
                "<displayname>KeePassBB2 AddIn</displayname>" +
                "<clsid>{" + Register.GUID + "}</clsid>" +
                "<access><database>KeePassBB2DatabaseObject</database></access>" +
                "<hasconfiguration>true</hasconfiguration></extensioninfo>";
        }

        bool checkPassword()
        {
            setProgress("Checking for saved credentials");
            if (password != null || keyfile != null) return true;
            PasswordForm pf = new PasswordForm();
            if (pf.ShowDialog() == DialogResult.OK)
            {
                password = pf.password;
                keyfile = pf.keyfile;
                return true;
            }
            return false;
        }
    }

    public class StatusMessage
    {
        public int rc;
        public String msg;
    }

}
