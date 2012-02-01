using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.IO;
using Microsoft.Win32;

namespace KeePassBB2AddIn
{
    public partial class ConfigForm : Form
    {
        public ConfigForm()
        {
            InitializeComponent();
            DatabasePath.Focus();
            AcceptButton = Ok;
            CancelButton = Cancel;
            StartPosition = FormStartPosition.CenterParent;
            RegistryKey rk = Registry.CurrentUser.CreateSubKey(@"SOFTWARE\" + Register.CompanyString + @"\" + Register.ProductString, RegistryKeyPermissionCheck.ReadWriteSubTree);
            DatabasePath.Text = (String)rk.GetValue(@"SyncFile", @"");
//            SyncSummary.Checked = ((int)rk.GetValue(@"SyncSummary", 0)) == 1;
            rk.Close();
        }

        private void Search_Click(object sender, EventArgs e)
        {

            OpenFileDialog openFileDialog1 = new OpenFileDialog();
            openFileDialog1.CheckFileExists = true;
            openFileDialog1.Title = "Select the KeePass Database File";
            openFileDialog1.InitialDirectory = DatabasePath.Text;
            openFileDialog1.Filter = "KeePass Database Files (*.kdbx)|*.kdbx|All Files (*.*)|*.*||";
            openFileDialog1.FilterIndex = 0;
            openFileDialog1.RestoreDirectory = true;
            openFileDialog1.AddExtension = true;
            openFileDialog1.Multiselect = false;

            if (openFileDialog1.ShowDialog() == DialogResult.OK)
            {
                DatabasePath.Text = openFileDialog1.FileName;
            }
        }

        private void ClearCredentials_Click(object sender, EventArgs e)
        {
            Processor.keyfile = null;
            Processor.password = null;
        }

        private void Cancel_Click(object sender, EventArgs e)
        {
            DialogResult = DialogResult.Cancel;
            Close();
        }

        private void Ok_Click(object sender, EventArgs e)
        {
            if (DatabasePath.Text.Length == 0)
            {
                MessageBox.Show("Database path can't be empty");
                return;
            }
            FileInfo fi = new FileInfo(DatabasePath.Text);
            if (!fi.Exists)
            {
                MessageBox.Show("The database '" + DatabasePath.Text + "' doesn't exist.");
                return;
            }

            RegistryKey rk = Registry.CurrentUser.CreateSubKey(@"SOFTWARE\" + Register.CompanyString + @"\" + Register.ProductString, RegistryKeyPermissionCheck.ReadWriteSubTree);
            rk.SetValue(@"SyncFile", DatabasePath.Text);
  //          rk.SetValue(@"SyncSummary", SyncSummary.Checked ? 1 : 0);
            rk.Close();
            DialogResult = DialogResult.OK;
            Close();
        }
    }
}
