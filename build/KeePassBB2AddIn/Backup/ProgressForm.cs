using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace KeePassBB2AddIn
{
    public partial class ProgressForm : Form
    {
        public ProgressForm()
        {
            InitializeComponent();
        }

        public void setProgressMsg(String msg)
        {
            ProgressMsg.Text = msg;
            progressBar1.PerformStep();
        }

        private void ProgressMsg_TextChanged(object sender, EventArgs e)
        {

        }

    }
}
