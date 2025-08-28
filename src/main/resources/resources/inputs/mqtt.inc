# Definição dos novos tipos de objeto para a paleta do JaamSim

Define ObjectType {
	MqttClient
	MqttSend
	MqttReceive
	MqttEntity
}

# Definição das classes Java e da paleta
# (Removemos as referências a IconModel e ImageFile para simplificar)

MqttClient 	JavaClass { com.jaamsim.Mqtt.MqttClient  } Palette { 'MQTT Objects' } DefaultSize { 0.5 0.5 0.5 m }
MqttSend 	JavaClass { com.jaamsim.Mqtt.MqttSend 	 } Palette { 'MQTT Objects' } DefaultSize { 0.5 0.5 0.5 m }
MqttReceive JavaClass { com.jaamsim.Mqtt.MqttReceive } Palette { 'MQTT Objects' } DefaultSize { 0.5 0.5 0.5 m }
MqttEntity 	JavaClass { com.jaamsim.Mqtt.MqttEntity  } Palette { 'MQTT Objects' } DefaultSize { 0.5 0.5 0.5 m }

# Descrições dos objetos

MqttClient 	Description { 'MQTT client object.' }
MqttSend 	Description { 'MQTT send operation object.' }
MqttReceive Description { 'MQTT receive operation object.' }
MqttEntity 	Description { 'MQTT entity object created from a message.' }