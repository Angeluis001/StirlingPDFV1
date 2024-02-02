# Pipeline Configuration and Usage Tutorial

## Whilst Pipelines are in alpha...
You must enable this alpha functionality by setting
```yaml
system:
  enableAlphaFunctionality: true
```
To true like in the above for your `/config/settings.yml` file, after restarting Stirling-PDF you should see both UI and folder scanning enabled.


## Steps to Configure and Use Your Pipeline

1. **Access Configuration**
   - Upon entering the screen, click on the **Configure** button.

2. **Enter Pipeline Name**
   - Provide a name for your pipeline in the designated field.

3. **Select Operations**
   - Choose the operations for your pipeline (e.g., **Split Pages**), then click **Add Operation**.

4. **Configure Operation Settings**
   - Input the necessary settings for each added operation. Settings are highlighted in yellow if customization is needed.

5. **Add More Operations**
   - You can add and adjust the order of multiple operations. Ensure each operation's settings are customized.

6. **Save Settings**
   - Click **Save Operation Settings** after customizing settings for each operation.

7. **Validate Pipeline**
   - Use the **Validation** button to check your pipeline. A green indicator signifies correct setup; a pop-out error indicates issues.

8. **Download Pipeline Configuration**
   - To use the configuration for folder scanning (or save it for future use and reupload it), you can also download a JSON file in this menu. You can also pre-load this for future use by placing it in ``/pipeline/defaultWebUIConfigs/``. It will then appear in the dropdown menu for all users to use.

9. **Submit Files for Processing**
   - If your pipeline is correctly set up close the configure menu, input the files and hit **Submit**.

10. **Note on Web UI Limitations**
    - The current web UI version does not support operations that require multiple different types of inputs, such as adding a separate image to a PDF.
