/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2021 Georg Hackenberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaamsim.Mqtt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jaamsim.ProcessFlow.LinkedComponent;
import com.jaamsim.events.EventHandle;
import com.jaamsim.events.ProcessTarget;
import com.jaamsim.input.EntityInput;
import com.jaamsim.input.InputAgent;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.StringInput;

public class MqttReceive extends LinkedComponent implements IMqttMessageListener {

	@Keyword(description = "The MQTT client.", exampleList = "[client]")
	private final EntityInput<MqttClient> client;
	{
		client = new EntityInput<>(MqttClient.class, "Client", KEY_INPUTS, null);
		client.setRequired(true);

		this.addInput(client);
	}

	@Keyword(description = "The MQTT topic.", exampleList = "some/mqtt/topic")
	private final StringInput topic;
	{
		topic = new StringInput("Topic", KEY_INPUTS, null);
		topic.setRequired(true);

		this.addInput(topic);
	}

	protected int numberGenerated;
	
	protected org.eclipse.paho.client.mqttv3.MqttClient mqtt;

	@Override
	public void lateInit() {
		try {
			numberGenerated = 0;
			
			mqtt = this.client.getValue().getClient();
			mqtt.subscribe(topic.getValue(), this);
		} catch (MqttException e) {
			error("The MQTT receive could not subscribe (reason code = " + e.getReasonCode() + ")");
		} catch (Exception e) {
			error("The MQTT receive could not subscribe (exception = " + e.getLocalizedMessage() + ")");
		}
	}

	@Override
	public void doEnd() {
		try {
			mqtt.unsubscribe(topic.getValue());
			mqtt = null;
		} catch (MqttException e) {
			//error("The MQTT receive could not unsubscribe (reason code = " + e.getReasonCode() + ")");
			if (e.getReasonCode() == 32104) {
				this.trace(1, "Could not unsubscribe because client was already disconnected. This is normal at the end of a run.");
			} else {
				// Para outros erros de MQTT, ainda reportamos como um erro.
				error("The MQTT receive could not unsubscribe (reason code = " + e.getReasonCode() + ")");
			}
		} catch (Exception e) {
			error("The MQTT receive could not unsubscribe (exception = " + e.getLocalizedMessage() + ")");
		}
	}

	//TESTE
/* 
		@Override
	public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
		try {
			// Capturamos o payload como 'final' para que a classe interna possa acessá-lo
			final String finalPayload = new String(message.getPayload());

			System.out.println(">>>> DEBUG: Mensagem recebida, agendando processo de teste...");
			System.out.println(">>>> DEBUG: Payload para o teste: " + finalPayload);

			// Usamos a sua estrutura original e correta para agendar a tarefa
			getJaamSimModel().getEventManager().scheduleProcessExternal(0L, 0, false, new ProcessTarget() {
				
				@Override
				public void process() {
					// Aqui dentro, executamos apenas o nosso teste mínimo
					try {
						System.out.println(">>>> INICIANDO TESTE DE ISOLAMENTO JSON...");
						
						// A única linha que realmente importa para testar o problema:
						new JSONObject(finalPayload);
						
						System.out.println(">>>> SUCESSO! A biblioteca JSON foi encontrada e funcionou!");

					} catch (Throwable t) {
						System.err.println("!!! FALHA NO TESTE DE ISOLAMENTO. Causa do erro de classpath confirmada.");
						t.printStackTrace();
					}
				}

				@Override
				public String getDescription() {
					// Uma descrição para o evento, útil para depuração
					return "MqttMessage.jsonIsolationTest";
				}
			}, new EventHandle());

		} catch (Exception e) {
			error("A recepção MQTT não pôde agendar a tarefa de teste (exceção = " + e.getLocalizedMessage() + ")");
		}
	}
*/

	@Override
	public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
		try {
			System.out.println(">>>> DEBUG: MENSAGEM RECEBIDA NO TOPICO: " + topic);
        	System.out.println(">>>> DEBUG: PAYLOAD: " + new String(message.getPayload()));
			
			String name = getName() + "_" + numberGenerated++;
			String payload = new String(message.getPayload());
			JSONObject json = new JSONObject(payload);

			
			getJaamSimModel().getEventManager().scheduleProcessExternal(0L, 0, false, new ProcessTarget() {
				@Override
				public void process() {
					try {
						System.out.println(">>>> DEBUG: O processo agendado foi executado com sucesso.");
						
						MqttEntity entity = InputAgent.generateEntityWithName(getJaamSimModel(), MqttEntity.class, null, name, null, true, false);
						
						entity.setTopic(topic);
						
						updateValue(entity, json);
						
						entity.earlyInit();
						
						nextComponent.getValue().addEntity(entity);
					
					} catch (Throwable t) {
						// Se QUALQUER coisa der errado no bloco try, o programa vem para cá em vez de quebrar
						System.err.println("");
						System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						System.err.println("!!! ERRO FATAL NO CALLBACK DO MQTTRECEIVE !!!");
						System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						t.printStackTrace(); // Este comando vai imprimir o erro exato e a linha onde ocorreu!
						System.err.println("");
					}
				}
				@Override
				public String getDescription() {
					return name + ".generate";
				}
			}, new EventHandle());
		} catch (Exception e) {
			error("The MQTT receive could not schedule (exception = " + e.getLocalizedMessage() + ")");
		}
	}

	protected void updateValue(MqttEntity message, JSONObject object) throws Exception {
		Object value = transformValue(object.get("value"));
		
		message.setValue(value);
	}
	
	private Object transformValue(Object value) throws Exception {
		if (value instanceof Boolean) {
			return value;
		} else if (value instanceof Integer) {
			return value;
		} else if (value instanceof Double) {
			return value;
		} else if (value instanceof String) {
			return value;
		} else if (value instanceof JSONObject) {
			Map<String, Object> map = new LinkedHashMap<>();
			for (String key : ((JSONObject) value).keySet()) {
				map.put(key, transformValue(((JSONObject) value).get(key)));
			}
			return map;
		} else if (value instanceof JSONArray) {
			List<Object> list = new ArrayList<>();
			for (int index = 0; index < ((JSONArray) value).length(); index++) {
				list.add(transformValue(((JSONArray) value).get(index)));
			}
			return list;
		} else {
			throw new Exception("Value type not supported: " + value.getClass().getName());
		}
	}

}