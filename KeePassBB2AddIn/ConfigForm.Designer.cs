namespace KeePassBB2AddIn
{
    partial class ConfigForm
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
            this.DatabasePath = new System.Windows.Forms.TextBox();
            this.Search = new System.Windows.Forms.Button();
            this.ClearCredentials = new System.Windows.Forms.Button();
            this.Ok = new System.Windows.Forms.Button();
            this.Cancel = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // DatabasePath
            // 
            this.DatabasePath.Location = new System.Drawing.Point(13, 41);
            this.DatabasePath.Name = "DatabasePath";
            this.DatabasePath.Size = new System.Drawing.Size(244, 20);
            this.DatabasePath.TabIndex = 0;
            // 
            // Search
            // 
            this.Search.Location = new System.Drawing.Point(272, 37);
            this.Search.Name = "Search";
            this.Search.Size = new System.Drawing.Size(36, 23);
            this.Search.TabIndex = 1;
            this.Search.Text = "...";
            this.Search.UseVisualStyleBackColor = true;
            this.Search.Click += new System.EventHandler(this.Search_Click);
            // 
            // ClearCredentials
            // 
            this.ClearCredentials.Location = new System.Drawing.Point(13, 80);
            this.ClearCredentials.Name = "ClearCredentials";
            this.ClearCredentials.Size = new System.Drawing.Size(185, 35);
            this.ClearCredentials.TabIndex = 3;
            this.ClearCredentials.Text = "Clear saved credentials now";
            this.ClearCredentials.UseVisualStyleBackColor = true;
            this.ClearCredentials.Click += new System.EventHandler(this.ClearCredentials_Click);
            // 
            // Ok
            // 
            this.Ok.Location = new System.Drawing.Point(85, 139);
            this.Ok.Name = "Ok";
            this.Ok.Size = new System.Drawing.Size(75, 23);
            this.Ok.TabIndex = 4;
            this.Ok.Text = "Ok";
            this.Ok.UseVisualStyleBackColor = true;
            this.Ok.Click += new System.EventHandler(this.Ok_Click);
            // 
            // Cancel
            // 
            this.Cancel.Location = new System.Drawing.Point(171, 139);
            this.Cancel.Name = "Cancel";
            this.Cancel.Size = new System.Drawing.Size(75, 23);
            this.Cancel.TabIndex = 5;
            this.Cancel.Text = "Cancel";
            this.Cancel.UseVisualStyleBackColor = true;
            this.Cancel.Click += new System.EventHandler(this.Cancel_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Enabled = false;
            this.label1.Location = new System.Drawing.Point(13, 13);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(163, 13);
            this.label1.TabIndex = 6;
            this.label1.Text = "Enter the kdbx file to synchronize";
            // 
            // ConfigForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(317, 178);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.Cancel);
            this.Controls.Add(this.Ok);
            this.Controls.Add(this.ClearCredentials);
            this.Controls.Add(this.Search);
            this.Controls.Add(this.DatabasePath);
            this.Name = "ConfigForm";
            this.Text = "KeePass for BlackBerry Configuration";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox DatabasePath;
        private System.Windows.Forms.Button Search;
        private System.Windows.Forms.Button ClearCredentials;
        private System.Windows.Forms.Button Ok;
        private System.Windows.Forms.Button Cancel;
        private System.Windows.Forms.Label label1;
    }
}