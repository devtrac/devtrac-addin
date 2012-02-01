namespace KeePassBB2AddIn
{
    partial class ProgressForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.ProgressMsg = new System.Windows.Forms.TextBox();
            this.progressBar1 = new System.Windows.Forms.ProgressBar();
            this.SuspendLayout();
            // 
            // ProgressMsg
            // 
            this.ProgressMsg.Location = new System.Drawing.Point(49, 6);
            this.ProgressMsg.Name = "ProgressMsg";
            this.ProgressMsg.ReadOnly = true;
            this.ProgressMsg.Size = new System.Drawing.Size(343, 20);
            this.ProgressMsg.TabIndex = 0;
            this.ProgressMsg.Text = "Initializing";
            this.ProgressMsg.TextChanged += new System.EventHandler(this.ProgressMsg_TextChanged);
            // 
            // progressBar1
            // 
            this.progressBar1.Location = new System.Drawing.Point(49, 35);
            this.progressBar1.Maximum = 6;
            this.progressBar1.Name = "progressBar1";
            this.progressBar1.Size = new System.Drawing.Size(343, 23);
            this.progressBar1.Step = 1;
            this.progressBar1.TabIndex = 1;
            // 
            // ProgressForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(452, 82);
            this.Controls.Add(this.progressBar1);
            this.Controls.Add(this.ProgressMsg);
            this.Name = "ProgressForm";
            this.Text = "KeePassBB2 Sync Progress";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox ProgressMsg;
        public System.Windows.Forms.ProgressBar progressBar1;

    }
}