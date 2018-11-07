/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tanzim Ahmed
 */
package dll;

public abstract interface DLLLogger
{
  public abstract void addSendLog(String paramString);
  
  public abstract void addReceivedLog(String paramString);
}

