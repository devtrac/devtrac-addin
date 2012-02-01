using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.IO;
using System.Security.Cryptography;

using System.Windows.Forms;

namespace KeePassBB2AddIn
{
    public partial class PasswordForm : Form
    {
        public String password;
        public String keyfile;
        public PasswordForm()
        {
            InitializeComponent();
            Password.Focus();
            AcceptButton = Ok;
            CancelButton = Cancel;
            StartPosition = FormStartPosition.CenterParent;
        }

        private void Ok_Click(object sender, EventArgs e)
        {
            password = this.Password.Text;
            keyfile = this.Filename.Text;
            if (password.Length == 0 && keyfile.Length == 0)
            {
                MessageBox.Show("Both password and keyfile can't be empty");
                return;
            }
            if (keyfile != null && keyfile.Length > 0)
            {
                FileInfo fi = new FileInfo(keyfile);
                if (!fi.Exists)
                {
                    MessageBox.Show("The keyfile doesn't exist.");
                    return;
                }
            }
            DialogResult = DialogResult.OK;
            Close();
        }

        private void Cancel_Click(object sender, EventArgs e)
        {
            DialogResult = DialogResult.Cancel;
            Close();
        }

    
        private void PasswordForm_Load(object sender, EventArgs e)
        {
        }

        private void Search_Click(object sender, EventArgs e)
        {
            OpenFileDialog openFileDialog1 = new OpenFileDialog();
            openFileDialog1.CheckFileExists = true;
            openFileDialog1.Title = "Select the KeePass Keyfile";
            openFileDialog1.Filter = "KeePass Keyfile (*.key)|*.key|All Files (*.*)|*.*||";
            openFileDialog1.FilterIndex = 0;
            openFileDialog1.RestoreDirectory = true;
            openFileDialog1.AddExtension = true;
            openFileDialog1.Multiselect = false;

            if (openFileDialog1.ShowDialog() == DialogResult.OK)
            {
                Filename.Text = openFileDialog1.FileName;
            }

        }

    }
}
